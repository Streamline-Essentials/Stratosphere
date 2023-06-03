package tv.quaint.stratosphere.users;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

public class MetaContainer implements Identifiable, SelfDatalizable<MetaContainer> {
    @Getter @Setter
    private String identifier;
    @Getter
    private String data;

    public MetaContainer setData(String data) {
        this.data = data;
        return this;
    }

    public MetaContainer(String identifier) {
        try {
            MetaContainer container = fromData(identifier);
            this.identifier = container.getIdentifier();
            this.data = container.getData();
        } catch (Exception e) {
            this.identifier = identifier;
            this.data = "";
        }
    }

    public MetaContainer(String identifier, String data) {
        this.identifier = identifier;
        this.data = data;
    }

    @Override
    public String toData() {
        return ">" + identifier + "=" + data + "<";
    }

    @Override
    public MetaContainer fromData(String data) {
        if (data.length() > 2) data = data.substring(1, data.length() - 1);
        String[] split = data.split("=", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid data for MetaContainer: " + data);
        }
        return new MetaContainer(split[0], split[1]);
    }
}
