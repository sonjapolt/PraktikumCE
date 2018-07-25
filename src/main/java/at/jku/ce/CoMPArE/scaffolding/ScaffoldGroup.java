package at.jku.ce.CoMPArE.scaffolding;

import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;

import java.util.*;

/**
 * Created by oppl on 25/11/2016.
 */
public class ScaffoldGroup {

    private Map<Integer,Scaffold> scaffolds;


    public ScaffoldGroup() {
        scaffolds = new HashMap<>();
    }

    public void addScaffold(int type, Scaffold scaffold) {
        scaffolds.put(new Integer(type), scaffold);
    }

    public Scaffold getScaffold(int type) {
        return scaffolds.get(new Integer(type));
    }

    public Scaffold getMostConcreteScaffoldForType(int type) {
        for (int i = type; i>0; i--) {
            Scaffold s = scaffolds.get(new Integer(i));
            if (s!=null) return s;
        }
        return null;
    }

    public boolean contains(Scaffold scaffold) {
        if (scaffolds.containsValue(scaffold)) return true;
        return false;
    }

    public Set<String> removeScaffoldAndMoreVagueFromGroup (Scaffold scaffold) {
        Integer toBeRemoved = new Integer(-1);
        Set<String> removedIDs = new HashSet<>();
        for (Integer i : scaffolds.keySet()) {
            if (scaffolds.get(i).equals(scaffold)) toBeRemoved = i;
        }
        if (toBeRemoved.intValue()!=-1) {
            for (int i = toBeRemoved.intValue(); i>0; i--) {
                Scaffold s = scaffolds.get(new Integer(i));
                if (s != null) {
                    removedIDs.add(s.getUniqueID());
                    scaffolds.remove(new Integer(i));
                }
            }
        }
        return removedIDs;
    }

    public String removeScaffoldFromGroup(Scaffold scaffold) {
        Integer toBeRemoved = new Integer(-1);;
        for (Integer i : scaffolds.keySet()) {
            if (scaffolds.get(i).equals(scaffold)) toBeRemoved = i;
        }
        if (toBeRemoved.intValue()!=-1) {
            scaffolds.remove(toBeRemoved);
            return scaffold.getUniqueID();
        }
        return null;
    }

    public Set<String> removeAllScaffoldsFromGroup() {
        Set<String> removedIDs = new HashSet<>();
        for (Integer i: scaffolds.keySet()) {
            removedIDs.add(scaffolds.get(i).getUniqueID());
        }
        scaffolds.clear();
        return removedIDs;
    }
}
