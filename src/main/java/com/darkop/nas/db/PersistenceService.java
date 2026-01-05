package com.darkop.nas.db;

import com.darkop.nas.model.records.BatchRun;
import com.darkop.nas.model.records.UploadSummary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PersistenceService {

    public void persist(BatchRun batchRun, List<UploadSummary> summaries) throws SQLException {

        try (Connection connection = DbConfig.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BatchRunDao batchRunDao = new BatchRunDao(connection);
                long runId = batchRunDao.save(batchRun);

                UserDailyUploadDao userDailyUploadDao = new UserDailyUploadDao(connection);
                userDailyUploadDao.save(runId, summaries);

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
