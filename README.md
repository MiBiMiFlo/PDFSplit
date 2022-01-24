# PDFSplit
A small tool using apache pdfbox to split a single PDF file into multiple based on a separator page.

The intention of the tool is to make batch scanning as easy as possible. The idea is to insert a custom separator page between the single documents.
PDFSplit now identifies the separator page and automatically splits on this page (the separator age is dropped).

PDFSplit allows to use a QR code on the separator page as well as special Text that either is identified by it's internal OCR (using Tesseract) or
by the OCR engine of the scanner (extracting the text with pdfbox).

# Swing User Interface

PDFSplit has a small (but useful) SWING based user interface. This interface allows to open a PDF document and shows the split output documents for preview.
Additionally it allows to easily drop pages (e.g. blank pages from double side scanning) from the split output documents.

# Configuration
PDFSplit is to be configured from the pdfsplit.cfg file that can either be located in the users home directory or in the application directory
(current directory at application start).

# Command Line:
PDFSplit can be executed with the following command line for batch processing:
<pre>
java -jar pdfsplit.jar [input_file.pdf]
</pre>

This will split the given PDF file and store single pdf files based on config location.

# Planned features
- SWING UI should get a nice looking Settings Dialog (incl. update of pdfsplit.cfg file)
- Possibly update to be more modular (e.g. support different OCR or pdf libraries)
- Allow output name masks and programatically defined output names)
- Allow to do OCR on split and save with OCR result text (make searchable PDFs while splitting)
