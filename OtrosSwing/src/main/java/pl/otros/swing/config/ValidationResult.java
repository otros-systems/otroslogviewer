package pl.otros.swing.config;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
  private final List<String> validationMessages;

  public ValidationResult() {
    super();
    validationMessages = new ArrayList<String>();
  }

  public boolean isValidationIsCorrect() {
    return validationMessages.size() == 0;
  }

  public void addErrorMessage(String message) {
    validationMessages.add(message);
  }

  public List<String> getErrorMessages() {
    return validationMessages;
  }
}
