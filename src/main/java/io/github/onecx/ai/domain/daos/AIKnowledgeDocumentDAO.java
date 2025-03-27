package io.github.onecx.ai.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import io.github.onecx.ai.domain.criteria.AIKnowledgeDocumentSearchCriteria;
import io.github.onecx.ai.domain.models.AIKnowledgeDocument;
import io.github.onecx.ai.domain.models.AIKnowledgeDocument_;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AIKnowledgeDocumentDAO extends AbstractDAO<AIKnowledgeDocument> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public AIKnowledgeDocument findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(AIKnowledgeDocument.class);
            var root = cq.from(AIKnowledgeDocument.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));

            EntityGraph graph = this.em.getEntityGraph(AIKnowledgeDocument.AI_KNOWLEDGE_DOCUMENT_LOAD);

            return this.getEntityManager().createQuery(cq).setHint(HINT_LOAD_GRAPH, graph).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(AIKnowledgeDocumentDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public PageResult<AIKnowledgeDocument> findAIKnowledgeDocumentsByCriteria(
            AIKnowledgeDocumentSearchCriteria knowledgeDocumentSearchCriteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(AIKnowledgeDocument.class);
            var root = cq.from(AIKnowledgeDocument.class);

            // use predicates to do not overwrite to other one
            List<Predicate> predicates = new ArrayList<>();
            addSearchStringPredicate(predicates, cb, root.get(AIKnowledgeDocument_.NAME),
                    knowledgeDocumentSearchCriteria.getName());
            addSearchStringPredicate(predicates, cb, root.get(AIKnowledgeDocument_.DOCUMENT_REF_ID),
                    knowledgeDocumentSearchCriteria.getDocumentRefId());
            if (knowledgeDocumentSearchCriteria.getStatus() != null) {
                addSearchStringPredicate(predicates, cb, root.get(AIKnowledgeDocument_.status.getName()),
                        knowledgeDocumentSearchCriteria.getStatus().name());
            }
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq,
                    Page.of(knowledgeDocumentSearchCriteria.getPageNumber(),
                            knowledgeDocumentSearchCriteria.getPageSize()))
                    .getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_SEARCH_AI_KNOWLEDGE_DOCUMENTS_BY_CRITERIA, ex);
        }
    }

    public enum ErrorKeys {
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_SEARCH_AI_KNOWLEDGE_DOCUMENTS_BY_CRITERIA
    }
}
