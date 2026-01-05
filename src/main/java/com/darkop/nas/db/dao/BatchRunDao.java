package com.darkop.nas.db.dao;

import com.darkop.nas.model.records.BatchRun;

import java.sql.*;

public class BatchRunDao {

    private static final String INSERT_SQL = """
            INSERT INTO batch_run (
                run_date,
                start_time,
                end_time,
                total_users_seen,
                total_files_seen,
                total_bytes_seen,
                total_files_sorted,
                total_files_failed,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;

    public BatchRunDao(Connection connection) {
        this.connection = connection;
    }

    public long insert(BatchRun batchRun) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, batchRun.runDate());

            ps.setObject(2, batchRun.startTime());
            ps.setObject(3, batchRun.endTime());

            ps.setInt(4, batchRun.totalUsersSeen());

            ps.setLong(5, batchRun.totalFilesSeen());
            ps.setLong(6, batchRun.totalBytesSeen());

            ps.setLong(7, batchRun.totalFilesSorted());
            ps.setLong(8, batchRun.totalFilesFailed());

            ps.setString(9, batchRun.status().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated run_id");
            }
        }
    }
}
