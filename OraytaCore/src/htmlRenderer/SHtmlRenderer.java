package htmlRenderer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import fileManager.SFileWriter;
import htmlRenderer.DCSSBuilder;

import settings.SettingsManager;
import tree.TreeNode;

import book.Book;
import book.contents.ChapterAddress;
import book.contents.IChapter;

public class SHtmlRenderer implements IHtmlRenderer
{
	/*
	 * Creates a unique Html file name for each book.
	 * NOTE: This is NOT unique per chapter, so this dosn't actually do caching. 
	 */
	private String genSuggestedSavePath(int bookID, Boolean isIndex)
	{
		String path = SettingsManager.getSettings().get_HTML_RENDERED_FILES_PATH();
		
		String prefix = "H";
		if (isIndex) prefix = "I";
		
		path += prefix + String.valueOf(bookID) + ".html";
		
		return path;
	}
	
	private URL saveToFile(HtmlPage page, String filePath)
	{
		URL url = null;
		File file = new File(filePath);
		
		try 
		{
			Boolean ok = (new SFileWriter()).writeToFile(filePath, page.getHtmlString(), true);
			if (ok) url = file.toURI().toURL();
		} 
		catch (MalformedURLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return url;
	}
	
	public URL renderFullBook(Book book) 
	{
		return renderFullBook(book, new ArrayList<Book>());
	}
	
	public URL renderFullBook(Book book, Collection<Book> otherBooks)
	{
		return saveToFile(genFullBookHtml(book, otherBooks), genSuggestedSavePath(book.getBookID(), false));
	}
	
	public HtmlPage genFullBookHtml(Book book, Collection<Book> otherBooks)
	{
		HtmlPage page = new HtmlPage();
		
		String htmlHead = HtmlMarkupBuilder.genHeader(book.getDisplayName(), new DCSSBuilder(book));
		page.setHtmlHead(htmlHead);
		
		String htmlBody = genChapterIndexHtml(book).getHtmlBody();
		htmlBody += renderChapterContents(book, book.getContents().getChapterContentsTree().data.getChapterAddress(), otherBooks);
		page.setHtmlBody(htmlBody);
		
		page.setHtmlEnd(HtmlMarkupBuilder.htmlEnd());
		
		return page;
	}
	
	public URL renderChapterIndex(Book book) 
	{
		return saveToFile(genChapterIndexHtml(book), genSuggestedSavePath(book.getBookID(), true));
	}
	
	private HtmlPage genChapterIndexHtml(Book book) 
	{
		HtmlPage page = new HtmlPage();
		
		String htmlHead = HtmlMarkupBuilder.genHeader(book.getDisplayName(), new DCSSBuilder(book));
		page.setHtmlHead(htmlHead);
		
		String htmlBody = "";
		//TODO: Improve!
		for(ChapterAddress addrr:book.getContents().getFlatIndex())
		{
			htmlBody += HtmlMarkupBuilder.genLinkToChapter(addrr);
		}
		page.setHtmlBody(htmlBody);
		
		page.setHtmlEnd(HtmlMarkupBuilder.htmlEnd());
		
		return page;
	}

	public URL renderChapter(Book book, ChapterAddress chapid) 
	{
		return renderChapter(book, chapid, new ArrayList<Book>()); 
	}
	
	public URL renderChapter(Book book, ChapterAddress chapid, Collection<Book> otherBooks) 
	{
		return saveToFile(genChapterHtml(book, chapid, otherBooks), genSuggestedSavePath(book.getBookID(), false));
	}
	
	public HtmlPage genChapterHtml(Book book, ChapterAddress chapid, Collection<Book> otherBooks) 
	{
		HtmlPage page = new HtmlPage();
		
		page.setHtmlHead(HtmlMarkupBuilder.genHeader(book.getDisplayName() + " - " + chapid.getTitle(),
				new DCSSBuilder(book)));
		
		page.setHtmlBody(renderChapterContents(book, chapid, otherBooks));
		
		page.setHtmlEnd(HtmlMarkupBuilder.htmlEnd());
		
		return page;
	}
	
	private String renderChapterContents(Book book, ChapterAddress chapid, Collection<Book> otherBooks)
	{
		String html = "";

		TreeNode<IChapter> baseNode = book.getContents().getChapterNodeByID(chapid.getUID());

		if (baseNode == null) return "Invalid chapter!";
		
		List<IChapter> chaps = baseNode.deepSiblingsList();

		for (IChapter chap:chaps)
		{
			ArrayList<String> chapLines = new ArrayList<String>(Arrays.asList(chap.text().split("\\r?\\n")));
			
			//Deal with low-level chap markers (only if they come at the beginning of the chapter)
			if (chapLines.size() > 1 && chapLines.get(0).trim().startsWith(HtmlMarkupBuilder.genHtmlMarkerComment()))
			{
				//TODO: This is generated when OBK built. Probably should move here
				html += chapLines.get(1);
				
				chapLines.remove(1);
				chapLines.remove(0);
			}
			
			html += HtmlMarkupBuilder.genChapTitle(chap);
			
			//TODO: Have this in base paragraph style
			//TODO: Add <BR>?
			for (String line:chapLines) html += line + "\n";
			
			//Add contents from all books we are supposed to weave in
			int c = 0;
			for (Book other:otherBooks)
			{
				IChapter otherChap = other.getContents().getChapterByID(chap.getUID());
				String chapTextNoMarkers = removeChapMarkerFromChapText(otherChap);

				if (!chapTextNoMarkers.isEmpty())
				{
                    if (otherBooks.size() > 1 && c == 0)
                        html += "<BR>\n";
					
					html += "<BR>\n" + HtmlMarkupBuilder.genSpanClassPrefix("W", c++);
					html += HtmlMarkupBuilder.genItalicBold(other.getDisplayNameWhenWeaved()) + "<BR>\n";
					
					html += chapTextNoMarkers;
					
					html += HtmlMarkupBuilder.genSpanSuffix() + "\n";
					
                    html += "<BR>\n";
				}
			}
		}
		
		return html;
	}
	
	private String removeChapMarkerFromChapText(IChapter chap)
	{
		if (chap == null) return "";
		
		String chapText = chap.text();
		ArrayList<String> chapLines = new ArrayList<String>(Arrays.asList(chapText.trim().split("\\r?\\n")));
		
		//Deal with low-level chap markers (only if they come at the beginning of the chapter)
		if (chapLines.size() > 1 && chapLines.get(0).trim().startsWith(HtmlMarkupBuilder.genHtmlMarkerComment()))
		{
			chapLines.remove(1);
			chapLines.remove(0);
		}
		
		StringBuilder builder = new StringBuilder();
		for(String s : chapLines) 
		{
			builder.append(s); 
			builder.append("\n"); 
		}
		
		String merged = "";
		if (builder.length() > 0) merged = builder.substring(0, builder.length()-1);
		
		return merged;
	}
}
