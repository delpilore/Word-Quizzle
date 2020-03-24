package source;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = 4780598293424730148L;
	
	private StatusCodes statusCode;
	
	public Response(String _response, StatusCodes _statusCode) {
		statusCode = _statusCode;
	}
	
	public StatusCodes getStatusCode() {
		return statusCode;
	}
	
}
