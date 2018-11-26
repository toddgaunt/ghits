CC = pdflatex
CFLAGS =

SRC = \
	macro.tex \
	report.tex

all: report.pdf

%.pdf: %.tex
	$(CC) $<

report.pdf: $(SRC)

clean:
	rm -f report.pdf report.log report.aux report.idx
