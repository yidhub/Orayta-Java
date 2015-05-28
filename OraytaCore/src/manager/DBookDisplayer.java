package manager;

import tree.ITreeIterator;
import tree.TreeNode;
import htmlRenderer.HtmlPage;
import htmlRenderer.IHtmlRenderer;
import book.Book;
import book.contents.ChapterAddress;
import book.contents.IChapter;
import bookBuilder.IBookBuildersFactory;
import bookBuilder.IBookContentsBuilder;
import bookmark.Bookmark;

public class DBookDisplayer implements IBookDisplayManager 
{
	IOraytaCore CoreManager;
	IHtmlRenderer Renderer;
	IBookBuildersFactory BuilderFactory;
	
	Book currentBook;
	TreeNode<IChapter> currentAddressNode;
	
	
	public DBookDisplayer(IOraytaCore core)
	{
		CoreManager = core;
		
		if (CoreManager == null) throw new IllegalArgumentException();
		
		Renderer = CoreManager.getHtmlRenderer();
		BuilderFactory = CoreManager.getBookBuildersFactory();
	}
	
	public HtmlPage displayBook(int bookID) 
	{
		Book b = CoreManager.getBookTree().getElementByID(bookID);
		currentBook = b;
		
		return displayBook(b);
	}

	public HtmlPage displayBook(Book book) 
	{
		currentBook = book;
		initBook(book);
		
		currentAddressNode = book.getContents().getChapterContentsTree();
		
		return Renderer.renderChapterIndex(book);
	}

	public HtmlPage displayBookAtAddress(Book book, ChapterAddress address) 
	{
		currentBook = book;
		currentAddressNode = book.getContents().getChapterNodeByID(address.getUID());
		
		initBook(book);
		//TODO: Deal with weaved display
		return Renderer.renderChapter(book, address);
	}
	
	private void initBook(Book book) 
	{
		if (book.getContents() != null) return;
		
		IBookContentsBuilder contentsBuilder = BuilderFactory.getContentsBuilder(book);
		book.setContents(contentsBuilder.buildBookContents(book));
	}

	public Boolean hasNextChapter() 
	{
		return (currentAddressNode.iterator().hasNext());
	}

	public Boolean hasPreviousChapter() 
	{
		return (currentAddressNode.iterator().hasPrevious());
	}

	public HtmlPage displayNextChapter() 
	{
		ITreeIterator<TreeNode<IChapter>> iter = currentAddressNode.iterator();
		if (iter.hasNext()) currentAddressNode = iter.next();
		
		if (currentAddressNode.data == null) return null;
		
		return displayBookAtAddress(currentBook, currentAddressNode.data.getChapterAddress());
	}

	public HtmlPage displayPreviousChapter() 
	{
		ITreeIterator<TreeNode<IChapter>> iter = currentAddressNode.iterator();
		if (iter.hasPrevious()) currentAddressNode = iter.previous();
		
		if (currentAddressNode.data == null) return null;
		
		return displayBookAtAddress(currentBook, currentAddressNode.data.getChapterAddress());
	}

	public Book getCurrentlyDisplayedBook() { return currentBook; }

	public ChapterAddress getCurrentlySetAddress() 
	{
		if (currentAddressNode.data == null) return null;

		return currentAddressNode.data.getChapterAddress();
	}

	public ChapterAddress getMostAccurateCurrentlyAddress() {
		// TODO Actually implement
		return getCurrentlySetAddress();
	}

	public HtmlPage displayLink(String codedUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	public HtmlPage displayBookmark(Bookmark bookmark) {
		// TODO Auto-generated method stub
		return null;
	}

}
