package io.github.onecx.ai.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AIKnowledgeDocumentDAOTest {
    @Inject
    AIKnowledgeDocumentDAO dao;

    @InjectMock
    EntityManager entityManager;

    @BeforeEach
    void beforeAll() {
        Mockito.when(entityManager.getCriteriaBuilder())
                .thenThrow(new RuntimeException("[AIKnowledgeDocumentDAOTest] Test technical error exception"));
    }

    @Test
    void findAIKnowledgeDocumentByIdExceptionTest() {
        methodExceptionTests(() -> dao.findById(null),
                AIKnowledgeDocumentDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
    }

    @Test
    void searchAIKnowledgeDocumentByCriteriaExceptionTest() {
        methodExceptionTests(() -> dao.findAIKnowledgeDocumentsByCriteria(null),
                AIKnowledgeDocumentDAO.ErrorKeys.ERROR_SEARCH_AI_KNOWLEDGE_DOCUMENTS_BY_CRITERIA);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
