package source;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 4780598293424730148L;
	
	private String response;
	
	public Response(String _response) {
		response = _response;
	}
	
	public String getResponse() {
		return response;
	}
}
