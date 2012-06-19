/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.sal.util;

import info.aduna.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.CommentImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.RatingImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;
import uk.ac.open.kmi.iserve.sal.model.review.Comment;
import uk.ac.open.kmi.iserve.sal.model.review.Rating;
import uk.ac.open.kmi.iserve.sal.model.review.Review;

public class LufConnector {

	private String lufUriString;

	private String lufSparqlEndpoint;

	private String ratingApiUri;

	private String commentApiUri;

	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	private static HttpClient client = new HttpClient(connectionManager);

	private String prefix = "PREFIX rev:<http://purl.org/stuff/rev#>\n" +
		"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

	public LufConnector(URI lufUri) {
		if ( lufUri.toString().endsWith("/")) {
			int lastSlash = lufUri.toString().lastIndexOf("/");
			this.lufUriString = lufUri.toString().substring(0, lastSlash);
		} else {
			this.lufUriString = lufUri.toString();
		}
		this.lufSparqlEndpoint = this.lufUriString + "/sparql?query=";
		this.ratingApiUri = this.lufUriString + "/api/ratings";
		this.commentApiUri = this.lufUriString + "/api/comments";
	}

	public List<Review> getReviews(URI serviceUri) throws HttpException, IOException, ParseException {
		List<Review> result = null;
		String queryString = prefix + "SELECT DISTINCT * WHERE {\n  " +
			serviceUri.toSPARQL() + " rev:hasReview ?review . \n" +
			"  ?review rev:reviewer ?reviewer . \n" +
			"  ?review rev:createdOn ?time . \n" +
			"  OPTIONAL { ?review rev:rating ?rating . \n" +
			"    ?review rev:minRating ?min . \n" +
			"    ?review rev:maxRating ?max . \n" +
			"  } \n" +
			"  OPTIONAL { ?review rev:text ?comment . } \n" +
			"}\n";
		String queryUriString = this.lufSparqlEndpoint + URLEncoder.encode(queryString, "UTF-8");
//		System.out.println("queryUriString: " + queryUriString);
		GetMethod queryMethod = new GetMethod(queryUriString);
		queryMethod.setRequestHeader("Accept", "application/sparql-results+xml");

		client.executeMethod(queryMethod);
		InputStream is = queryMethod.getResponseBodyAsStream();
		String lufResponse = IOUtil.readString(is);
//		System.out.println("lufResponse: " + lufResponse);
		if ( lufResponse.contains("<sparql") ) {
			QueryResult qr = SPARQLQueryResultParser.parse(lufResponse);			
			result = parseReviews(qr);
		}
		queryMethod.releaseConnection();
		return result;
	}

	private void setReviewerTime(Review review, String reviewerString, String timeString) throws ParseException {
		if ( reviewerString != null && !reviewerString.equalsIgnoreCase("") ) {
			review.setReviewerUri(new URIImpl(reviewerString));
		}
		if ( timeString != null && !timeString.equalsIgnoreCase("") ) {
			// FIXME: maybe not work for timezone begin with -
			if ( !timeString.contains(" -") ) {
				timeString = timeString.replaceAll(" ", "+");
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			Date time = dateFormat.parse(timeString);
			review.setCreateTime(time);
		}
	}

	private List<Review> parseReviews(QueryResult qr) throws ParseException {
		List<Review> result = new ArrayList<Review>();
		if ( qr == null  || qr.getQueryRows() == null || qr.getQueryRows().size() <= 0 )
			return result;
		for ( QueryRow row : qr.getQueryRows() ) {
			String reviewerString = row.getValue("reviewer");
			String timeString = row.getValue("time");
			String ratingString = row.getValue("rating");
			String minRatingString = row.getValue("min");
			String maxRatingString = row.getValue("max");
			String commentString = row.getValue("comment");
			if ( ratingString != null && !ratingString.equalsIgnoreCase("") ) {
				Rating rating = new RatingImpl();
				rating.setValue(Double.valueOf(ratingString));
//				System.out.println(rating.getValue());
				if ( minRatingString != null && !minRatingString.equalsIgnoreCase("") ) {
					rating.setMinValue(Double.valueOf(minRatingString));
//					System.out.println(rating.getMinValue());
				}
				if ( maxRatingString != null && !maxRatingString.equalsIgnoreCase("") ) {
					rating.setMaxValue(Double.valueOf(maxRatingString));
//					System.out.println(rating.getMaxValue());
				}
				setReviewerTime(rating, reviewerString, timeString);
				result.add(rating);
			} else if ( commentString != null && !commentString.equalsIgnoreCase("") ) {
				Comment comment = new CommentImpl(commentString);
				setReviewerTime(comment, reviewerString, timeString);
				result.add(comment);
//				System.out.println(comment.getContent());
			}
		}
		return result;
	}

	public boolean reviewService(URI serviceUri, URI userUri, Rating rating, Comment comment) throws HttpException, IOException {
		if ( null == serviceUri || null == serviceUri.toString() || serviceUri.toString().equals("") ||
				null == userUri || null == userUri.toString() || userUri.toString().equals("") ) {
			return false;
		}
		if ( rating != null && rating.getValue() != 0 ) {
			PostMethod postRating = new PostMethod(ratingApiUri);
			postRating.addParameter("itemId", serviceUri.toString());
			postRating.addParameter("userId", userUri.toString());
			postRating.addParameter("rating", "" + rating.getValue());
			int statusCode = client.executeMethod(postRating);
			if ( statusCode != HttpStatus.SC_OK ) {
				return false;
			}
//			System.out.println(postRating.getResponseBodyAsString());
		}

		if ( comment != null && comment.getContent() != null && !comment.equals("") ) {
			PostMethod postComment = new PostMethod(commentApiUri);
			postComment.addParameter("itemId", serviceUri.toString());
			postComment.addParameter("userId", userUri.toString());
			postComment.addParameter("comment", "" + comment.getContent());
			int statusCode = client.executeMethod(postComment);
			if ( statusCode != HttpStatus.SC_OK ) {
				return false;
			}
//			System.out.println(postComment.getResponseBodyAsString());
		}

		return true;
	}

	public void setProxy(String proxyHost, int proxyPort) {
		if ( null == client ) {
			return;
		}
		client.getHostConfiguration().setProxy(proxyHost, proxyPort);
	}

	public static void main(String[] args) {
		LufConnector lufConnector = new LufConnector(new URIImpl("http://soa4all.isoco.net/luf"));
//		lufConnector.setProxy("wwwcache.open.ac.uk", 80);
		try {
			lufConnector.getReviews(new URIImpl("http://iserve.kmi.open.ac.uk:8080/resource/services/f8b9adc8-899e-4b5e-8496-3921e575c13f#LastFmFriends"));
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
