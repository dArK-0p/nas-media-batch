package com.darkop.nas.db;

import com.darkop.nas.model.UploadSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDailyUploadDao {

    private static final String INSERT_SQL = """
        INSERT INTO user_daily_upload (
            run_id,
            username,
            files_uploaded,
            bytes_uploaded
        )
        VALUES (?, ?, ?, ?)
        """;

    private final Connection connection;

    public UserDailyUploadDao(Connection connection) {
        this.connection = connection;
    }

    public void save(long runId, List<UploadSummary> summaries) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            for (UploadSummary summary : summaries) {
                ps.setLong(1, runId);
                ps.setString(2, summary.username());
                ps.setLong(3, summary.fileCount());
                ps.setLong(4, summary.byteCount());

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }
}
