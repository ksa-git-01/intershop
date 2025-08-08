TRUNCATE TABLE order_items, orders, cart, item RESTART IDENTITY CASCADE;

DO $$
DECLARE
    i INTEGER;
    titles TEXT[] := ARRAY['Smartphone', 'Laptop', 'Headphones', 'Keyboard', 'Mouse', 'Monitor', 'Tablet', 'Camera', 'Speaker', 'Router'];
    descriptions TEXT[] := ARRAY['Premium quality', 'Latest model', 'Wireless', 'Ergonomic design', 'High performance', 'Energy efficient', 'Compact size', 'Noise cancelling', 'Water resistant', 'Bluetooth enabled'];
    brands TEXT[] := ARRAY['Apple', 'Samsung', 'Sony', 'LG', 'HP', 'Dell', 'Lenovo', 'Asus', 'Acer', 'Xiaomi'];
BEGIN
    FOR i IN 1..50 LOOP
        INSERT INTO item (
            title,
            description,
            filename,
            count,
            price,
            created_at,
            updated_at
        ) VALUES (
            brands[(i % array_length(brands, 1)) + 1] || ' ' || titles[(i % array_length(titles, 1)) + 1] || ' ' || (i % 10 + 1),
            descriptions[(i % array_length(descriptions, 1)) + 1] || ' ' || titles[(i % array_length(titles, 1)) + 1],
            'product_' || (i % 20 + 1) || '.jpg',
            (random() * 100)::INTEGER + 1,
            (random() * 1000 + 50)::NUMERIC(10,2),
            CURRENT_TIMESTAMP - (random() * 30)::INTEGER * INTERVAL '1 day',
            CASE WHEN random() > 0.3 THEN NULL ELSE CURRENT_TIMESTAMP - (random() * 15)::INTEGER * INTERVAL '1 day' END
        );
    END LOOP;
END $$;
^^^ END OF SCRIPT ^^^

DO $$
DECLARE
    item RECORD;
    item_count INTEGER;
BEGIN
    FOR item IN (
        SELECT id
        FROM item
        ORDER BY random()
        LIMIT 5
    ) LOOP
        item_count := (random() * 9)::INTEGER + 1;

        INSERT INTO cart (
            item_id,
            count,
            created_at,
            updated_at
        ) VALUES (
            item.id,
            item_count,
            CURRENT_TIMESTAMP - (random() * 7)::INTEGER * INTERVAL '1 day',
            CASE WHEN random() > 0.5 THEN NULL ELSE CURRENT_TIMESTAMP - (random() * 3)::INTEGER * INTERVAL '1 day' END
        );
    END LOOP;
END $$;
^^^ END OF SCRIPT ^^^

DO $$
DECLARE
    order_id BIGINT;
    items_in_order INTEGER;
    i INTEGER;
    j INTEGER;
    random_item_id BIGINT;
    item_price NUMERIC(10,2);
    item_count INTEGER;
BEGIN
    FOR i IN 1..5 LOOP
        INSERT INTO orders (created_at, updated_at)
        VALUES (
            CURRENT_TIMESTAMP - (random() * 14)::INTEGER * INTERVAL '1 day',
            CASE WHEN random() > 0.5 THEN NULL ELSE CURRENT_TIMESTAMP - (random() * 7)::INTEGER * INTERVAL '1 day' END
        ) RETURNING id INTO order_id;

        items_in_order := (random() * 4)::INTEGER + 1;

        FOR j IN 1..items_in_order LOOP
            SELECT id, price INTO random_item_id, item_price
            FROM item
            ORDER BY random()
            LIMIT 1;

            item_count := (random() * 4)::INTEGER + 1;

            INSERT INTO order_items (
                item_id,
                order_id,
                count,
                price,
                created_at
            ) VALUES (
                random_item_id,
                order_id,
                item_count,
                item_price,
                CURRENT_TIMESTAMP - (random() * 3)::INTEGER * INTERVAL '1 day'
            );
        END LOOP;
    END LOOP;
END $$;
^^^ END OF SCRIPT ^^^