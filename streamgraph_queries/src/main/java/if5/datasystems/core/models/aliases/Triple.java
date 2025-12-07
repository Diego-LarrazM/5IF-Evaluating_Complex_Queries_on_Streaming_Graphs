package if5.datasystems.core.models.aliases;

import lombok.Data;

public record Triple<A,B,C>(A first, B second, C third) {}

