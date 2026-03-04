package com.converters; // Ou un autre package approprié pour les convertisseurs

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;

/**
 * Convertisseur JPA pour persister et récupérer des objets java.time.Duration
 * sous forme de Long (représentant les minutes) dans la base de données.
 */
@Converter(autoApply = false) // Ne pas appliquer automatiquement à tous les Duration
public class DurationConverter implements AttributeConverter<Duration, Long> {

    /**
     * Convertit un objet Duration en un Long (nombre de minutes) pour la persistance en base de données.
     * @param duration L'objet Duration à convertir.
     * @return Le nombre total de minutes de la durée, ou null si la durée est null.
     */
    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        if (duration == null) {
            return null;
        }
        // Nous allons stocker la durée en minutes.
        // Si vous avez besoin d'une précision plus fine (secondes, millisecondes),
        // ajustez cette conversion (par exemple, duration.toSeconds() ou duration.toMillis()).
        return duration.toMinutes();
    }

    /**
     * Convertit un Long (nombre de minutes) de la base de données en un objet Duration.
     * @param minutes Le nombre de minutes stocké en base de données.
     * @return L'objet Duration correspondant, ou null si les minutes sont null.
     */
    @Override
    public Duration convertToEntityAttribute(Long minutes) {
        if (minutes == null) {
            return null;
        }
        return Duration.ofMinutes(minutes);
    }
}
