INSERT INTO catalogs (id, name, slug) VALUES
    (1000, 'Инструменты', 'instrumenty-test'),
    (1001, 'Крепёж',      'krepezh-test');

INSERT INTO sub_catalogs (id, name, slug, catalog_id) VALUES
    (2000, 'Дрели',        'dreli-test',        1000),
    (2001, 'Шуруповёрты',  'shurupoverty-test',  1000),
    (2002, 'Болты',        'bolty-test',         1001);
