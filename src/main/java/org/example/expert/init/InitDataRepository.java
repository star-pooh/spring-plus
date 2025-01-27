package org.example.expert.init;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InitDataRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveAll(List<User> userList, int batchSize) {
        String sql = "INSERT INTO users (email, password, user_role, nickname) " +
                "VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql,
                userList,
                batchSize,
                (PreparedStatement ps, User user) -> {
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, user.getUserRole().toString());
                    ps.setString(4, user.getNickname());
                });

//        jdbcTemplate.batchUpdate(sql,
//                new BatchPreparedStatementSetter() {
//                    @Override
//                    public void setValues(PreparedStatement ps, int i) throws SQLException {
//                        User user = userList.get(i);
//                        ps.setString(1, user.getEmail());
//                        ps.setString(2, user.getPassword());
//                        ps.setString(3, user.getUserRole().toString());
//                        ps.setString(4, user.getNickname());
//                    }
//
//                    @Override
//                    public int getBatchSize() {
//                        return userList.size();
//                    }
//                });
    }
}
