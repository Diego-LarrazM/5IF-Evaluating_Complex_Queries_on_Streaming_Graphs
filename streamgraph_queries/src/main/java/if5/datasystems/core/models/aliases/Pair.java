package if5.datasystems.core.models.aliases;

import java.io.Serializable;

public record Pair<A,B>(A first, B second) implements Serializable {}
