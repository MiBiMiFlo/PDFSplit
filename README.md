# PDFSplit
A small tool using apache pdfbox to split a single PDF file into multiple based on a separator page.

The intention of the tool is to make batch scanning as easy as possible. In the current version (0.1) the PDF need to be scanned with OCR to enable searching for a particular separator text.

In a later version it is possibly planned to support a special separator image (e.g. a QR code) or at least allow to configure a OCR software (like teseract: https://tesseract-ocr.github.io/)

# Command Line:
The compiled tool can be executed with the following command line:

<pre>
java -jar pdfsplit.jar [input_file.pdf] <separator>
</pre>

This will split the given PDF file and store single pdf files next to the input file.