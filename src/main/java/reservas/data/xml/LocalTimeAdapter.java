package reservas.data.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {
    @Override
    public LocalTime unmarshal(String v) {
        return LocalTime.parse(v);
    }

    @Override
    public String marshal(LocalTime v) {
        return v.toString();
    }
}