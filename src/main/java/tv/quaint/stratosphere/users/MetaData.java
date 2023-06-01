package tv.quaint.stratosphere.users;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.AtomicString;
import tv.quaint.thebase.lib.re2j.Matcher;
import tv.quaint.utils.MatcherUtils;

import java.util.concurrent.ConcurrentSkipListSet;

public class MetaData implements SelfDatalizable<MetaData> {
    public static final String FRONTING = "[{{[";
    public static final String BACKING = "]}}]";

    @Getter @Setter
    private ConcurrentSkipListSet<MetaContainer> meta;

    public MetaData(ConcurrentSkipListSet<MetaContainer> meta) {
        this.meta = meta;
    }

    public MetaData() {
        this(new ConcurrentSkipListSet<>());
    }

    public MetaData(String data) {
        this();
        MetaData d = fromData(data);
        this.meta = d.getMeta();
    }

    @Override
    public String toData() {
        StringBuilder builder = new StringBuilder(FRONTING);
        for (MetaContainer container : meta) {
            builder.append(container.toData());
        }
        builder.append(BACKING);
        return builder.toString();
    }

    @Override
    public MetaData fromData(String data) {
        Matcher matcher = MatcherUtils.matcherBuilder("\\[\\{\\{\\[(.*?)\\]\\}\\}\\]", data);
        ConcurrentSkipListSet<MetaContainer> meta = new ConcurrentSkipListSet<>();

        while (matcher.find()) {
            meta.add(new MetaContainer(matcher.group(1)));
        }

        return new MetaData(meta);
    }

    public boolean hasMeta(String key) {
        return meta.stream().anyMatch(container -> container.getIdentifier().equals(key));
    }

    public String getMeta(String key) {
        AtomicString atomicString = new AtomicString();
        meta.stream().filter(container -> container.getIdentifier().equals(key)).forEach(container -> atomicString.set(container.getData()));
        return atomicString.get();
    }

    public void setMeta(String key, String data) {
        AtomicString atomicString = new AtomicString();
        meta.stream().filter(container -> container.getIdentifier().equals(key)).forEach(container -> atomicString.set(container.getData()));

        if (atomicString.get() != null) {
            meta.removeIf(container -> container.getIdentifier().equals(key));
        }
        meta.add(new MetaContainer(key, data));
    }
}
