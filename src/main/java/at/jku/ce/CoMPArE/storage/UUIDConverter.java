package at.jku.ce.CoMPArE.storage;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.util.UUID;

/**
 * Created by oppl on 14/01/2017.
 */
public class UUIDConverter implements SingleValueConverter {

        public String toString(Object obj) {
            return ((UUID) obj).toString();
        }

        public Object fromString(String name) {
            return UUID.fromString(name);
        }

        public boolean canConvert(Class type) {
            return type.equals(UUID.class);
        }

}
