package com.isaborosa.biblioteca.service.recommendation;

import java.util.List;
import java.util.Set;

record RecommendationSignals(List<String> topGenres, List<String> topAuthors, Set<String> ownedSignatures) {
}
