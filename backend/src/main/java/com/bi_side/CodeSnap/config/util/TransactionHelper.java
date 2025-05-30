package com.bi_side.CodeSnap.config.util;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class TransactionHelper {
    private final DataSourceTransactionManager txManager;

    public TransactionHelper(DataSourceTransactionManager txManager) {
        this.txManager = txManager;
    }

    public void runInTransaction(Runnable runnable) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);

        try {
            runnable.run();
            txManager.commit(status);
        } catch (Exception ex) {
            txManager.rollback(status);
            throw ex;
        }
    }

    public void runInNewTransaction(Runnable runnable) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);

        try {
            runnable.run();
            txManager.commit(status);
        } catch (Exception ex) {
            txManager.rollback(status);
            throw ex;
        }
    }
}
