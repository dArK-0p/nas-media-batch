package com.darkop.nas.db.dao;

import com.darkop.nas.model.records.SortResult;
import com.darkop.nas.model.records.UploadSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDailyUploadDao {

    private static final String INSERT_SQL = """
            INSERT INTO user_daily_upload (
                run_id,
                username,
                files_uploaded,
                bytes_uploaded,
                files_sorted,
                files_failed
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;

    public UserDailyUploadDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(long runId, UploadSummary uploadSummary, SortResult sortResult) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setLong(1, runId);
            ps.setString(2, uploadSummary.username());

            ps.setLong(3, uploadSummary.fileCount());
            ps.setLong(4, uploadSummary.byteCount());

            ps.setLong(5, sortResult.filesSorted());
            ps.setLong(6, sortResult.failures().size());

            ps.executeUpdate();
        }
    }
}
