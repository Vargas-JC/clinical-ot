package com.app.hubble.config;

import com.app.hubble.enumeration.AppointmentStatus;
import com.app.hubble.enumeration.ExamType;
import com.app.hubble.enumeration.NotificationType;
import com.app.hubble.enumeration.PaymentMethod;
import com.app.hubble.enumeration.PaymentStatus;
import com.app.hubble.enumeration.SpecialityType;
import com.app.hubble.enumeration.UserRole;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.EnumWriteSupport;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import java.util.List;

@Configuration
public class PostgresqlR2dbcConfig {
    @Bean
    @Primary
    public ConnectionFactory connectionFactory(
            @Value("${spring.r2dbc.url}") String url,
            @Value("${spring.r2dbc.username}") String username,
            @Value("${spring.r2dbc.password}") String password,
            @Value("${spring.r2dbc.pool.initial-size:10}") int poolInitialSize,
            @Value("${spring.r2dbc.pool.max-size:10}") int poolMaxSize
    ) {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(url);
        PostgresqlConnectionConfiguration.Builder builder = PostgresqlConnectionConfiguration.builder();
        builder.host((String) options.getRequiredValue(ConnectionFactoryOptions.HOST));
        Object port = options.getValue(ConnectionFactoryOptions.PORT);
        if (port instanceof Integer portValue) {
            builder.port(portValue);
        }
        builder.database((String) options.getRequiredValue(ConnectionFactoryOptions.DATABASE));
        Object schema = options.getValue(Option.valueOf("schema"));
        if (schema instanceof String schemaValue) {
            builder.schema(schemaValue);
        }
        builder.username(username);
        builder.password(password);
        builder.codecRegistrar(EnumCodec.builder()
                .withEnum("user_role", UserRole.class)
                .withEnum("speciality_type", SpecialityType.class)
                .withEnum("appointment_status", AppointmentStatus.class)
                .withEnum("payment_method", PaymentMethod.class)
                .withEnum("payment_status", PaymentStatus.class)
                .withEnum("exam_type", ExamType.class)
                .withEnum("notification_type", NotificationType.class)
                .build());
        ConnectionFactory postgresql = new PostgresqlConnectionFactory(builder.build());
        return new ConnectionPool(ConnectionPoolConfiguration.builder(postgresql)
                .initialSize(poolInitialSize)
                .maxSize(poolMaxSize)
                .build());
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = List.of(
                new UserRoleWritingConverter(),
                new SpecialityTypeWritingConverter(),
                new AppointmentStatusWritingConverter(),
                new PaymentMethodWritingConverter(),
                new PaymentStatusWritingConverter(),
                new ExamTypeWritingConverter(),
                new NotificationTypeWritingConverter()
        );
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }

    @WritingConverter
    private static class UserRoleWritingConverter extends EnumWriteSupport<UserRole> {
    }

    @WritingConverter
    private static class SpecialityTypeWritingConverter extends EnumWriteSupport<SpecialityType> {
    }

    @WritingConverter
    private static class AppointmentStatusWritingConverter extends EnumWriteSupport<AppointmentStatus> {
    }

    @WritingConverter
    private static class PaymentMethodWritingConverter extends EnumWriteSupport<PaymentMethod> {
    }

    @WritingConverter
    private static class PaymentStatusWritingConverter extends EnumWriteSupport<PaymentStatus> {
    }

    @WritingConverter
    private static class ExamTypeWritingConverter extends EnumWriteSupport<ExamType> {
    }

    @WritingConverter
    private static class NotificationTypeWritingConverter extends EnumWriteSupport<NotificationType> {
    }
}
