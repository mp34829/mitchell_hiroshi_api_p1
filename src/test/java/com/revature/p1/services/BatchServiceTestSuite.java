package com.revature.p1.services;

import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.util.UserSession;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import org.junit.*;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BatchServiceTestSuite {

    BatchService sut;

    private UserSession mockUserSession;
    private BatchRepository mockBatchRepo;

    @Before
    public void beforeEachTest() {
        mockUserSession = mock(UserSession.class);
        mockBatchRepo = mock(BatchRepository.class);
        sut = new BatchService(mockBatchRepo, mockUserSession);
    }

    @After
    public void afterEachTest() {
        sut = null;
    }

    @Test
    public void isUserValid_returnsTrue_givenValidUser() {

        // Arrange
        Batch validBatch = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);

        // Act
        boolean actualResult = sut.isBatchValid(validBatch);

        // Assert
        Assert.assertTrue("Expected user to be considered valid!", actualResult);

    }

    @Test
    public void isUserValid_returnsFalse_givenUserWithNullOrEmptyFirstName() {

        // Arrange
        Batch invalidBatch1 = new Batch(null, "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        Batch invalidBatch2 = new Batch("", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        Batch invalidBatch3 = new Batch("    ", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);

        // Act
        boolean actualResult1 = sut.isBatchValid(invalidBatch1);
        boolean actualResult2 = sut.isBatchValid(invalidBatch2);
        boolean actualResult3 = sut.isBatchValid(invalidBatch3);

        /* Assert */
        Assert.assertFalse("Batch short name cannot be null!", actualResult1);
        Assert.assertFalse("Batch short name cannot be an empty string!", actualResult2);
        Assert.assertFalse("Batch short name cannot be only whitespace!", actualResult3);

    }

    @Test
    public void register_returnsSuccessfully_whenGivenValidUser() {

        // Arrange
        Batch expectedResult = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        Batch validBatch = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        when(mockBatchRepo.save(any())).thenReturn(expectedResult);

        // Act
        Batch actualResult = sut.addBatch(validBatch);

        // Assert
        Assert.assertEquals(expectedResult, actualResult);
        verify(mockBatchRepo, times(1)).save(any());

    }

    @Test(expected = InvalidRequestException.class)
    public void register_throwsException_whenGivenInvalidUser() {

        // Arrange
        Batch invalidBatch = new Batch(null, "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);

        // Act
        try {
            sut.addBatch(invalidBatch);
        } finally {
            // Assert
            verify(mockBatchRepo, times(0)).save(any());
        }

    }

    @Test(expected = ResourcePersistenceException.class)
    public void register_throwsException_whenGivenBatchWithDuplicateShortName() {

        // Arrange
        Batch existingUser = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        Batch duplicate = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"), Collections.EMPTY_LIST);
        when(mockBatchRepo.findById(duplicate.getShortName())).thenReturn(existingUser);

        // Act
        try {
            sut.addBatch(duplicate);
        } finally {
            // Assert
            verify(mockBatchRepo, times(1)).findById(duplicate.getShortName());
            verify(mockBatchRepo, times(0)).save(duplicate);
        }

    }

}
