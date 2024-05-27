# Import the rdflib library
from rdflib import Graph, URIRef, Literal
from difflib import Differ


# Create an RDF graph
g = Graph()

# Define RDF terms (URIs and Literals)
city1 = URIRef("http://example.org/cities#City1")
city2 = URIRef("http://example.org/cities#City2")
city3 = URIRef("http://example.org/cities#City3")
population = URIRef("http://example.org/ontology#population")
label = URIRef("http://www.w3.org/2000/01/rdf-schema#label")

# Add triples to the graph
g.add((city1, label, Literal("New York")))
g.add((city1, population, Literal(8398748)))
g.add((city2, label, Literal("Los Angeles")))
g.add((city2, population, Literal(3990456)))
g.add((city3, label, Literal("Chicago")))
g.add((city3, population, Literal(2705994)))

copied_graph = Graph()
copied_graph += g 

# Make changes to the copied graph
new_population = 9000000
copied_graph.set((city1, population, Literal(new_population)))

# Serialize and print the RDF graph in Turtle format
print(g.serialize(format="turtle"))


old_serialized = g.serialize(format="turtle")
new_serialized = copied_graph.serialize(format="turtle")

# Perform differencing using difflib
differ = Differ()
diff = list(differ.compare(old_serialized.splitlines(), new_serialized.splitlines()))

# Identify differences (added, removed, and modified triples)
added = [line[2:] for line in diff if line.startswith('+ ')]
removed = [line[2:] for line in diff if line.startswith('- ')]
modified = [line[2:] for line in diff if line.startswith('? ')]

# Print the differences
print("Added Triples:")
for triple in added:
    print(triple)

print("\nRemoved Triples:")
for triple in removed:
    print(triple)

print("\nModified Triples:")
for triple in modified:
    print(triple)

# Query the RDF graph using SPARQL
# query = """
#     SELECT ?city ?label ?population
#     WHERE {
#         ?city rdf:label ?label ;
#               ontology:population ?population .
#     }
# """

# # Execute the query and print the results
# results = g.query(query)
# for row in results:
#     city, label, population = row
#     print(f"City: {label}, Population: {population}")

# Close the RDF graph
g.close()