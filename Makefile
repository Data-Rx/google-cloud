GC=google-cloud-0.3.2.jar

install:
	lein compile
	lein install
	lein localrepo coords resources/$(GC)
	lein localrepo install --pom  pom.xml \
	target/$(GC) genekim/google-cloud 0.3.2
