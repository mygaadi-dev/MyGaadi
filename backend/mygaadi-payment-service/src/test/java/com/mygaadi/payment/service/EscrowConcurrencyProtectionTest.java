package com.mygaadi.payment.service;

import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.repository.EscrowRepository;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class EscrowConcurrencyProtectionTest {
    @Test
    void escrowUsesOptimisticVersionAndRepositoryLocking() throws Exception {
        Field versionField = Escrow.class.getDeclaredField("version");
        Method lockMethod = EscrowRepository.class.getMethod("findByBookingIdForUpdate", Long.class);

        assertNotNull(versionField.getAnnotation(Version.class));
        assertNotNull(lockMethod.getAnnotation(Lock.class));
    }
}
