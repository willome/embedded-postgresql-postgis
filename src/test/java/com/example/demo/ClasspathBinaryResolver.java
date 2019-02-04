package com.example.demo;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;

import com.opentable.db.postgres.embedded.PgBinaryResolver;

public class ClasspathBinaryResolver implements PgBinaryResolver {

    @Override
    public InputStream getPgBinary(String system, String machineHardware) throws IOException {
        ClassPathResource resource = new ClassPathResource(String.format("postgresql-10.6-postgis-2.5.1-%s-%s.txz", system, machineHardware));
        return resource.getInputStream();
    }

}
