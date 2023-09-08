package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Error extends Response{
    String error;
    public Error(String error){
        this.setResult(false);
        this.error = error;
    }
}
