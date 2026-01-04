package com.darkop.nas.db;

import com.darkop.nas.model.BatchRun;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BatchRunDao {

    private static final String INSERT_SQL = """
        INSERT INTO batch_run (
            run_date,
            start_time,
            end_time,
            total_users,
            total_files_seen,
            total_bytes_seen,
            status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private final Connection connection;

    public BatchRunDao(Connection connection) {
        this.connection = connection;
    }

    public long save(BatchRun batchRun) throws SQLException {
        try (PreparedStatement ps =
                     connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, batchRun.runDate());
            ps.setObject(2, batchRun.startTime());
            ps.setObject(3, batchRun.endTime());
            ps.setInt(4, batchRun.totalUsers());
            ps.setLong(5, batchRun.totalFilesSeen());
            ps.setLong(6, batchRun.totalBytesSeen());
            ps.setString(7, batchRun.status().name());

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
