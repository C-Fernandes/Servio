package com.ufrn.ppgti.servio.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ufrn.ppgti.servio.dto.request.ServiceSearchRequestDTO;
import com.ufrn.ppgti.servio.model.Locality;
import com.ufrn.ppgti.servio.model.ProviderProfile;
import com.ufrn.ppgti.servio.model.Review;
import com.ufrn.ppgti.servio.model.Service;
import com.ufrn.ppgti.servio.model.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Monta os filtros dinâmicos da busca avançada de serviços. Apenas os campos
 * preenchidos viram restrição, então o mesmo método atende desde a listagem sem
 * filtro nenhum até a combinação de todos eles.
 */
public final class ServiceSpecifications {

    private ServiceSpecifications() {
    }

    public static Specification<Service> withFilters(ServiceSearchRequestDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.isFalse(root.get("deleted")));

            String term = normalize(filters.getTerm());
            if (term != null) {
                String pattern = "%" + term.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filters.getCategoryId()));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
            }

            if (filters.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
            }

            String city = normalize(filters.getCity());
            String state = normalize(filters.getState());

            // A localização do serviço é a do prestador que o oferece: o join só
            // entra quando há filtro, para não excluir prestadores sem endereço.
            if (city != null || state != null) {
                Join<Service, ProviderProfile> provider = root.join("provider");
                Join<ProviderProfile, User> user = provider.join("user");
                Join<User, Locality> locality = user.join("locality");

                if (city != null) {
                    predicates.add(cb.equal(cb.lower(locality.get("city")), city.toLowerCase()));
                }

                if (state != null) {
                    predicates.add(cb.equal(cb.lower(locality.get("state")), state.toLowerCase()));
                }
            }

            Double minRating = filters.getMinRating();
            if (minRating != null && minRating > 0) {
                Subquery<Double> averageRating = query.subquery(Double.class);
                Root<Review> review = averageRating.from(Review.class);

                averageRating.select(cb.coalesce(cb.avg(review.get("rating")), 0.0));
                averageRating.where(cb.equal(review.get("order").get("service").get("id"), root.get("id")));

                predicates.add(cb.greaterThanOrEqualTo(averageRating, minRating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
