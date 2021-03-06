package se.sogeti.umea.cvconverter.adapter.client.http.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sogeti.umea.cvconverter.adapter.client.http.json.CurriculumVitaeImpl;
import se.sogeti.umea.cvconverter.application.ConversionException;

@Path("/pdf/{id}")
public class PdfResource extends Resource {

	private final static Logger LOG = LoggerFactory
			.getLogger(PdfResource.class);

	private final static HashMap<Integer, byte[]> pdfByteArrays = new HashMap<>();
	private static int counter = 0;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public int createPdfFromJson(@PathParam("id") final long id, String jsonCv) {
		
		byte[] convert = null;

		ObjectMapper mapper = new ObjectMapper();

		CurriculumVitaeImpl cv;
		Integer generatedId = null;
		try {
						
			cv = mapper.readValue(jsonCv, CurriculumVitaeImpl.class);
			cv.decorateTags();
			LOG.debug("Creating PDF for " + cv.getProfile().getName() + " ... ");
			LOG.debug("Cover Image: " + cv.getCoverImage());
			convert = service.convert(id, cv);
			
			generatedId = generateId();
			LOG.debug("Created PDF for " + cv.getProfile().getName() + " with id: " + generatedId);
			pdfByteArrays.put(generatedId, convert);
		} catch (NoSuchElementException | IllegalArgumentException e) {
			LOG.error("Error converting cv (layoutId=" + id + ").", e);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND)
					.entity(e.getMessage()).build());
		} catch (IOException | ConversionException e) {
			LOG.error(createErrorLogMessage(id, jsonCv), e);
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		
		return generatedId != null ? generatedId.intValue() : -1;
	}

	@GET
	@Produces({ "application/pdf" })
	public Response getPdf(@PathParam("id") Integer uniqueId) {
		
		final byte[] pdfAsBytes = pdfByteArrays.remove(uniqueId);

		if (pdfAsBytes == null) {
			LOG.debug("Could not find pdf with id " + uniqueId);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND)
					.entity("Could not find pdf with id " + uniqueId).build());
		}

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				output.write(pdfAsBytes);
			}
		};

		return Response.ok().entity(stream).build();
	}

	private String createErrorLogMessage(final long id, String jsonCv) {
		return "Error converting cv (id=" + id + ", cvJson_length="
				+ (jsonCv != null ? jsonCv.length() : "null") + ").";
	}

	private Integer generateId() {
		int i = counter++;		
		return new Integer(i);
	}
}
