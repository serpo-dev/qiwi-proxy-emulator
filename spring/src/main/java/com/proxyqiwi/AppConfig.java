// package com.proxyqiwi.qiwiproxyemulator;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.core.JdbcTemplate;
// import javax.sql.DataSource;

// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.jdbc.datasource.DriverManagerDataSource;
// // import javax.sql.DataSource;

// @Configuration
// public class AppConfig {

    
// //     @Bean
// //     public DataSource dataSource() {
// //         DriverManagerDataSource dataSource = new DriverManagerDataSource();
// //         dataSource.setDriverClassName("org.postgresql.Driver");
// //         dataSource.setUrl("jdbc:postgresql://localhost:5432/qiwi-proxy-emulator");
// //         dataSource.setUsername("postgres");
// //         dataSource.setPassword("psql");
// //         return dataSource;
// //     }

//     @Bean
//     public JdbcTemplate jdbcTemplate(DataSource dataSource) {
//         return new JdbcTemplate(dataSource);
//     }
// }

package com.proxyqiwi.qiwiproxyemulator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}