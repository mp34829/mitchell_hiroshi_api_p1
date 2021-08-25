package com.revature.p1.services;



import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import org.json.simple.JSONObject;
import org.junit.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BatchServiceTestSuite {

    BatchService sut;

    private BatchRepository mockBatchRepo;

    @Before
    public void beforeEachTest() {
        mockBatchRepo = mock(BatchRepository.class);
        sut = new BatchService(mockBatchRepo);
    }

    @After
    public void afterEachTest() {
        sut = null;
    }

    @Test
    public void isUserValid_returnsTrue_givenValidUser() {

        // Arrange
        Batch validBatch = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));

        // Act
        boolean actualResult = sut.isBatchValid(validBatch);

        // Assert
        Assert.assertTrue("Expected user to be considered valid!", actualResult);

    }

    @Test
    public void isUserValid_returnsFalse_givenUserWithNullOrEmptyFirstName() {

        // Arrange
        Batch invalidBatch1 = new Batch(null, "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
        Batch invalidBatch2 = new Batch("", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
        Batch invalidBatch3 = new Batch("    ", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));

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
        Batch expectedResult = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
        Batch validBatch = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
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
        Batch invalidBatch = new Batch(null, "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));

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
        Batch existingUser = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
        Batch duplicate = new Batch("valid", "valid", "valid", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.parse("2021-07-01T00:00:00Z"));
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

    @Test
    public void listUsableBatches_returnsUseableBatch_whenUseableBatchAvailable(){
        Batch batch = new Batch("valid", "valid", "Enabled", "valid", Instant.parse("2021-07-01T00:00:00Z"), Instant.MAX);
        List<Batch> allBatches = new ArrayList<>(Arrays.asList(batch));
        when(mockBatchRepo.listAllBatches()).thenReturn(allBatches);
        List<Batch> expectedResult = allBatches;

        List<Batch> actualResult = sut.listUsableBatches();

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test(expected = InvalidRequestException.class)
    public void removeBatch_throwsInvalidRequestException_whenNonExistentShortnamePassed(){
        when(mockBatchRepo.findById("shortname")).thenReturn(null);

        sut.removeBatch("shortname");
    }

    @Test
    public void editBatch_updatesBatchFields_whenFieldsUpdated(){
        Batch batch = new Batch("shortName", "name", "status", "description", Instant.now(), Instant.MAX);
        JSONObject json = new JSONObject();
        json.put("name", "edit");
        json.put("status", "edit");
        json.put("description", "edit");
        json.put("registrationStart", "2016-05-28T17:39:44.937Z");
        json.put("registrationEnd", "2016-05-28T17:39:44.937Z");

        when(mockBatchRepo.update(batch, batch.getShortName())).thenReturn(true);
        when(sut.getBatchByID(batch.getShortName())).thenReturn(batch);

        sut.editBatch(batch.getShortName(), json);

        Assert.assertEquals(batch.getName(), "edit");
        Assert.assertEquals(batch.getStatus(), "edit");
        Assert.assertEquals(batch.getDescription(), "edit");
        Assert.assertEquals(batch.getRegistrationStart(), Instant.parse("2016-05-28T17:39:44.937Z"));
        Assert.assertEquals(batch.getRegistrationEnd(), Instant.parse("2016-05-28T17:39:44.937Z"));
    }


}
