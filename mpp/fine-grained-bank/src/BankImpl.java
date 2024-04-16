import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * @author Ruslan Zaripov
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(final int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    @Override
    public long getAmount(final int index) {
        try {
            accounts[index].lock.lock();
            return accounts[index].amount;
        } finally {
            accounts[index].lock.unlock();
        }
    }

    @Override
    public long getTotalAmount() {
        try {
            long sum = 0;
            for (Account account : accounts) {
                account.lock.lock();
                sum += account.amount;
            }
            return sum;
        } finally {
            for (Account account : accounts) {
                account.lock.unlock();
            }
        }
    }

    @Override
    public long deposit(final int index, final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        Account account = accounts[index];
        account.lock.lock();
        try {
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            account.amount += amount;
            return account.amount;
        } finally {
            account.lock.unlock();
        }
    }

    @Override
    public long withdraw(final int index, final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        Account account = accounts[index];
        account.lock.lock();
        try {
            if (account.amount - amount < 0) {
                throw new IllegalStateException("Underflow");
            }
            account.amount -= amount;
            return account.amount;
        } finally {
            account.lock.unlock();
        }
    }

    @Override
    public void transfer(final int fromIndex, final int toIndex, final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
        if (fromIndex == toIndex) {
            throw new IllegalArgumentException("fromIndex == toIndex");
        }
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        if (fromIndex < toIndex) {
            from.lock.lock();
            to.lock.lock();
        } else {
            to.lock.lock();
            from.lock.lock();
        }
        try {
            if (amount > from.amount) {
                throw new IllegalStateException("Underflow");
            }
            else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            from.amount -= amount;
            to.amount += amount;
        } finally {
            to.lock.unlock();
            from.lock.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    static class Account {
        private final ReentrantLock lock = new ReentrantLock();
        /*
         * Amount of funds in this account.
         */
        private long amount;
    }
}
