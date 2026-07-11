package api.requests;

import api.validation.ValidMobile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LearnerRequest {

    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @Email
    String email;

    @ValidMobile //custom validation
    String mobile;

}