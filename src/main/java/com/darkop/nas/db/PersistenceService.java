package com.darkop.nas.db;

import com.darkop.nas.db.dao.BatchRunDao;
import com.darkop.nas.db.dao.FileFailureDao;
import com.darkop.nas.db.dao.UserDailyUploadDao;
import com.darkop.nas.model.records.BatchRun;
import com.darkop.nas.model.records.FileFailure;
import com.darkop.nas.model.records.SortResult;
import com.darkop.nas.model.records.UploadSummary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistenceService {

    private final Connection connection;

    public PersistenceService(Connection connection) {
        this.connection = connection;
    }

    public void persistBatch(BatchRun batchRun, List<UploadSummary> uploadSummaries, List<SortResult> sortResults) throws SQLException {

        try {
            connection.setAutoCommit(false);

            // 1. Persist batch run
            BatchRunDao batchRunDao = new BatchRunDao(connection);
            long runId = batchRunDao.insert(batchRun);

            // 2. Persist per-user upload + sort summary
            UserDailyUploadDao userDailyUploadDao = new UserDailyUploadDao(connection);

            Map<String, SortResult> sortResultByUser = sortResults.stream().collect(Collectors.toMap(SortResult::username, sr -> sr));

            for (UploadSummary uploadSummary : uploadSummaries) {

                SortResult sortResult = sortResultByUser.get(uploadSummary.username());

                if (sortResult == null) {
                    throw new IllegalStateException("Missing SortResult for user: " + uploadSummary.username());
                }

                userDailyUploadDao.insert(runId, uploadSummary, sortResult);
            }

            // 3. Collect and persist file failures (if any)
            List<FileFailure> allFailures = new ArrayList<>();

            for (SortResult sortResult : sortResults) {
                if (sortResult.failures() != null) {
                    allFailures.addAll(sortResult.failures());
                }
            }

            if (!allFailures.isEmpty()) {
                FileFailureDao fileFailureDao = new FileFailureDao(connection);
                fileFailureDao.insertAll(runId, allFailures);
            }

            connection.commit();

        } catch (Exception e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(true);
        }
    }
}
