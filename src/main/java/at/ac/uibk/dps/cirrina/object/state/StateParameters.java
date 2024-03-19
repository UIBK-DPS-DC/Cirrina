package at.ac.uibk.dps.cirrina.object.state;

import at.ac.uibk.dps.cirrina.object.action.Action;
import java.util.List;
import java.util.Optional;

record StateParameters(
    String name,
    boolean isInitial,
    boolean isTerminal,
    List<Action> entryActions,
    List<Action> exitActions,
    List<Action> whileActions,
    boolean isAbstract,
    boolean isVirtual,
    Optional<State> baseState
) {

}