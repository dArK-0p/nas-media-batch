package com.darkop.nas.db.dao;

import com.darkop.nas.model.records.FileFailure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FileFailureDao {

    private static final String INSERT_SQL = """
            INSERT INTO file_failure (
                run_id,
                username,
                file_path,
                file_size_bytes,
                failure_type,
                failure_stage,
                action_taken,
                recorded_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;

    public FileFailureDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * Persist all file failures for a batch run.
     */
    public void insertAll(long runId, List<FileFailure> failures) throws SQLException {

        if (failures == null || failures.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            for (FileFailure failure : failures) {

                ps.setLong(1, runId);
                ps.setString(2, failure.username());
                ps.setString(3, failure.path());
                ps.setLong(4, failure.size());

                ps.setString(5, failure.type().name());
                ps.setString(6, failure.stage().name());
                ps.setString(7, failure.actionTaken().name());

                ps.setObject(8, failure.timestamp());

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }
}
