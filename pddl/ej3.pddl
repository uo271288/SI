(define (domain blocksword)
(:predicates
(sin_nada_encima ?x)
(encima_mesa ?x)
(encima_bloque ?x ?y)
)
(:action apilar
:parameters (?ob ?underob)
:precondition (and (sin_nada_encima ?underob) (sin_nada_encima ?ob) (encima_mesa ?ob))
:effect (and (not(sin_nada_encima ?underob)) (encima_bloque ?ob ?underob) (not(encima_mesa ?ob)))
)
(:action desapilar
:parameters (?ob ?underob)
:precondition (and (sin_nada_encima ?ob) (encima_bloque ?ob ?underob))
:effect (and (encima_mesa ?ob) (not(encima_bloque ?ob ?underob)) (sin_nada_encima ?underob))
))