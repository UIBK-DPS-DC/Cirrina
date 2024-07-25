package at.ac.uibk.dps.cirrina.runtime.job;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import org.junit.jupiter.api.Test;

class JobDescriptionParserTest {

  @Test
  void testJobDescription() {
    final var jobDescriptionPkl = DefaultDescriptions.jobDescription;

    assertDoesNotThrow(() -> {
      final var jobDescription = new JobDescriptionParser().parse(jobDescriptionPkl);

      assertEquals("stateMachine1", jobDescription.stateMachineName);
      assertEquals(1, jobDescription.localData.size());
      assertEquals(0, jobDescription.bindEventInstanceIds.size());
      assertEquals(1, jobDescription.serviceImplementations.length);
      assertEquals("runtime", jobDescription.runtimeName);
    });
  }
}
