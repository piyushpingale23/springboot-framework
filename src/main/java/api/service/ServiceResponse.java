package api.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceResponse<T> {

    String message;
    T data;

}
