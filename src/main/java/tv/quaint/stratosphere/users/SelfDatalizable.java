package tv.quaint.stratosphere.users;

public interface SelfDatalizable<T extends SelfDatalizable<?>> extends Datalizable {
    public T fromData(String data);
}
