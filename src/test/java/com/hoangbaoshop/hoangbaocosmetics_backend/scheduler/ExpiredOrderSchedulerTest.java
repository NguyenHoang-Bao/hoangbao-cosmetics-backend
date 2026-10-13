package com.hoangbaoshop.hoangbaocosmetics_backend.scheduler;

import com.hoangbaoshop.hoangbaocosmetics_backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredOrderSchedulerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ExpiredOrderScheduler expiredOrderScheduler;

    @Test
    void testRun_CallsCancelExpiredBankTransferOrders() {
        when(orderService.cancelExpiredBankTransferOrders()).thenReturn(3);

        expiredOrderScheduler.run();

        verify(orderService, times(1)).cancelExpiredBankTransferOrders();
    }
}
