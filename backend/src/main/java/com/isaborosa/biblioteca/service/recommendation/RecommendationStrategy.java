package com.isaborosa.biblioteca.service.recommendation;

import com.isaborosa.biblioteca.dto.RecommendationDto;
import java.util.List;

/**
 * Porta para a geracao de recomendacoes. A implementacao atual usa regras
 * simples (autores mais bem avaliados); uma estrategia baseada em modelo
 * pode ser plugada depois sem mudar o contrato do endpoint.
 */
public interface RecommendationStrategy {

    List<RecommendationDto> recommend();
}
