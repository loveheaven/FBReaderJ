
#include <cstdlib>
#include <cstring>

#include <ZLInputStream.h>
#include <ZLStringUtil.h>
#include <ZLFileImage.h>

#include <ZLTextParagraph.h>

extern "C"
{
#include <fitz.h>
#include <mupdf.h>
}
#include "../../library/Book.h"

#include "PdfBridge.h"

renderdocument_t * PdfDocument_open(char * filename, char * password)
{
	fz_error error;
		fz_obj *obj;
		renderdocument_t *doc;
		int fitzmemory = 512 * 1024;

		//char * filename = (char*)(file.path().c_str());


		doc = (renderdocument_t*)fz_malloc(sizeof(renderdocument_t));
		if(!doc) {
			ERROR("Out of Memory");
			goto cleanup;
		}

		/* initialize renderer */

		error = fz_newrenderer(&doc->rast, pdf_devicergb, 0, fitzmemory);
		if (error) {
			DEBUG("Cannot create new renderer");
			goto cleanup;
		}

		/*
		 * Open PDF and load xref table
		 */

		doc->xref = pdf_newxref();
		error = pdf_loadxref(doc->xref, filename);
		if (error) {
			/* TODO: plug into fitz error handling */
			fz_catch(error, "trying to repair");
			DEBUG("Corrupted file '%s', trying to repair", filename);
			INFO("Corrupted file '%s', trying to repair", filename);
			error = pdf_repairxref(doc->xref, filename);
			if (error) {
				ERROR("PDF file is corrupted");
				goto cleanup;
			}
		}
		DEBUG("PdfCorrected file '%s', trying to repair", filename);
		error = pdf_decryptxref(doc->xref);
		if (error) {
			DEBUG("Cannot decrypt XRef table");
			goto cleanup;
		}

		DEBUG("pdf_needspassword!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		/*
		 * Handle encrypted PDF files
		 */

		if (pdf_needspassword(doc->xref)) {
			if(strlen(password)) {
				int ok = pdf_authenticatepassword(doc->xref, password);
				if(!ok) {
					DEBUG("Wrong password given");
					goto cleanup;
				}
			} else {
				DEBUG("PDF needs a password!");
				goto cleanup;
			}
		}

		DEBUG("pdffz_dictgets root!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		/*
		 * Load document metadata (at some point this might be implemented
		 * in the muPDF lib itself)
		 */

		obj = fz_dictgets(doc->xref->trailer, "Root");
		doc->xref->root = fz_resolveindirect(obj);
		if (!doc->xref->root) {
			fz_throw("syntaxerror: missing Root object");
			DEBUG("PDF syntax: missing \"Root\" object");
			goto cleanup;
		}
		fz_keepobj(doc->xref->root);

		obj = fz_dictgets(doc->xref->trailer, "Info");
		doc->xref->info = fz_resolveindirect(obj);
		if (doc->xref->info) {
			fz_keepobj(doc->xref->info);
		}

	cleanup:


		DEBUG("PdfDocument.nativeOpen(): return handle = %p", doc);
	return doc;
}

void PdfDocument_getInfo(renderdocument_t *doc, Book& pdfBook) {
	fz_obj *obj;
	if(!doc || !doc->xref || !doc->xref->info) return;

	obj = fz_dictgets(doc->xref->info, "Title");
	if (fz_isstring(obj)) {

		DEBUG("pdfhasTitle: %s!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",pdf_toutf8(obj) );
		pdfBook.setTitle(pdf_toutf8(obj));
	}

	obj = fz_dictgets(doc->xref->info, "Author");
	if (fz_isstring(obj)) {
		DEBUG("pdfhasAuthor %s !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", pdf_toutf8(obj));
		pdfBook.addAuthor(pdf_toutf8(obj));
	}
}
void PdfDocument_free(renderdocument_t *doc)
{
	if(doc) {
		if (doc->xref->store)
			pdf_dropstore(doc->xref->store);

		pdf_closexref(doc->xref);

		fz_droprenderer(doc->rast);

		fz_free(doc);
	}
}

renderpage_t * PdfPage_open(renderdocument_t* doc, int pageno)
{
	renderpage_t *page;
	fz_error error;
	fz_obj *obj;

	page = (renderpage_t*)fz_malloc(sizeof(renderpage_t));
	if(!page) {
		DEBUG("Out of Memory");
		return NULL;
	}

	pdf_flushxref(doc->xref, 0);
	obj = pdf_getpageobject(doc->xref, pageno);
	error = pdf_loadpage(&page->page, doc->xref, obj);
	if (error) {
		DEBUG("error loading page");
		goto cleanup;
	}

cleanup:
	/* nothing yet */

	DEBUG("PdfPage.nativeOpenPage(): return handle = %p", page);
	return page;
}

void PdfPage_free(renderpage_t* page)
{
	if(page) {
		if (page->page)
			pdf_droppage(page->page);

		fz_free(page);
	}
}

