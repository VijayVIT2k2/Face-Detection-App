package vijay.vlr2k2.facedetectionfinaltrial;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONException;

@ParseClassName("ParseClassName")
public class ParseServerClass extends ParseObject {

    public ParseServerClass() {
        // A default constructor is required.
    }

    // Define getters and setters for your properties
    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile file) {
        put("image", file);
    }

    public float[] getFaceVector() throws JSONException {
        // Add similar methods for other properties
        return (float[]) getJSONArray("faceVector").get(0); // Assuming "faceVector" is a JSON array
    }

    public void setFaceVector(float[] vector) {
        put("faceVector", vector);
    }

    // Add other properties as needed
}
