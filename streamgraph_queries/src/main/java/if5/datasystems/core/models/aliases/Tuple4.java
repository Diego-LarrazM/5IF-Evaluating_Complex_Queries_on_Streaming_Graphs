package if5.datasystems.core.models.aliases;

import java.io.Serializable;

public record Tuple4<A,B,C,D>(A first, B second, C third, D fourth) implements Serializable {}

