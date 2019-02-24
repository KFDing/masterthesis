PRIMARIES:=thesis.pdf
BUILDDIR:=.build
EXTRADIRS:=chapters literature figures styles logos

.PHONY:all $(PRIMARIES) $(patsubst %,$(BUILDDIR)/%,$(PRIMARIES))
all:$(PRIMARIES)

$(PRIMARIES):%.pdf:$(BUILDDIR)/%.pdf
	cp '$<' .

$(patsubst %,$(BUILDDIR)/%,$(PRIMARIES)):%:
	latexmk -output-directory="$(BUILDDIR)" -pdf "$(patsubst $(BUILDDIR)/%.pdf,%.tex,$@)"

$(BUILDDIR):
	mkdir -p '$(BUILDDIR)'

.PHONY:almostclean
almostclean:
	touch $(BUILDDIR); rm -rf $(BUILDDIR)

.PHONY:clean
clean:almostclean
	for f in $(PRIMARIES); do [ -f "$$f" ] && rm "$$f"; done
